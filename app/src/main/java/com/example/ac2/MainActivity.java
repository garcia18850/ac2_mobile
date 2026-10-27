package com.example.ac2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ac2.adapter.MediaAdapter;
import com.example.ac2.model.MediaItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MediaAdapter.OnMediaClickListener {

    private TextView tvFormTitle;
    private TextInputLayout tilNome, tilAno, tilNota;
    private TextInputEditText etNome, etAno, etNota;
    private Spinner spTipo, spGenero;
    private MaterialCheckBox cbAssistido;
    private Button btnSalvar;
    private MaterialButton btnCancelar;
    private NestedScrollView nestedScrollView;

    private TextInputEditText etBusca;
    private Spinner spFiltroTipo, spOrdenacao;
    private MaterialCheckBox cbFiltroAssistidos;
    private RecyclerView rvMedias;
    private TextView tvEmptyState, tvListHeader;

    private MediaAdapter adapter;
    private final List<MediaItem> allMediaList = new ArrayList<>();
    private final List<MediaItem> filteredMediaList = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    private String editingMediaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            FirebaseApp.initializeApp(this);
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        inicializarViews();
        configurarSpinners();
        configurarRecyclerView();
        configurarListeners();
        carregarDadosFirebase();
    }

    private void inicializarViews() {
        View mainView = findViewById(R.id.main);
        if (mainView instanceof NestedScrollView) {
            nestedScrollView = (NestedScrollView) mainView;
        }

        tvFormTitle = findViewById(R.id.tvFormTitle);
        tilNome = findViewById(R.id.tilNome);
        tilAno = findViewById(R.id.tilAno);
        tilNota = findViewById(R.id.tilNota);
        etNome = findViewById(R.id.etNome);
        etAno = findViewById(R.id.etAno);
        etNota = findViewById(R.id.etNota);
        spTipo = findViewById(R.id.spTipo);
        spGenero = findViewById(R.id.spGenero);
        cbAssistido = findViewById(R.id.cbAssistido);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);

        etBusca = findViewById(R.id.etBusca);
        spFiltroTipo = findViewById(R.id.spFiltroTipo);
        spOrdenacao = findViewById(R.id.spOrdenacao);
        cbFiltroAssistidos = findViewById(R.id.cbFiltroAssistidos);
        rvMedias = findViewById(R.id.rvMedias);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvListHeader = findViewById(R.id.tvListHeader);
    }

    private void configurarSpinners() {
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(
                this, R.array.tipos_array, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapterTipo);

        ArrayAdapter<CharSequence> adapterGenero = ArrayAdapter.createFromResource(
                this, R.array.generos_array, android.R.layout.simple_spinner_item);
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenero.setAdapter(adapterGenero);

        ArrayAdapter<CharSequence> adapterFiltroTipo = ArrayAdapter.createFromResource(
                this, R.array.filtro_tipos_array, android.R.layout.simple_spinner_item);
        adapterFiltroTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroTipo.setAdapter(adapterFiltroTipo);

        ArrayAdapter<CharSequence> adapterOrdenacao = ArrayAdapter.createFromResource(
                this, R.array.ordenacao_array, android.R.layout.simple_spinner_item);
        adapterOrdenacao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrdenacao.setAdapter(adapterOrdenacao);
    }

    private void configurarRecyclerView() {
        adapter = new MediaAdapter(this, filteredMediaList, this);
        rvMedias.setLayoutManager(new LinearLayoutManager(this));
        rvMedias.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnSalvar.setOnClickListener(v -> salvarOuAtualizarMedia());
        btnCancelar.setOnClickListener(v -> limparFormulario());

        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltrosEOrdenacao();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltrosEOrdenacao();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spFiltroTipo.setOnItemSelectedListener(filterListener);
        spOrdenacao.setOnItemSelectedListener(filterListener);
        cbFiltroAssistidos.setOnCheckedChangeListener((buttonView, isChecked) -> aplicarFiltrosEOrdenacao());
    }

    private void carregarDadosFirebase() {
        if (db == null) return;

        firestoreListener = db.collection("filmes_series")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this, getString(R.string.msg_erro_firebase) + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allMediaList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MediaItem item = doc.toObject(MediaItem.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                allMediaList.add(item);
                            }
                        }
                    }
                    aplicarFiltrosEOrdenacao();
                });
    }

    private void salvarOuAtualizarMedia() {
        if (!validarFormulario()) return;

        String nome = etNome.getText().toString().trim();
        String tipo = spTipo.getSelectedItem().toString();
        String genero = spGenero.getSelectedItem().toString();
        int ano = Integer.parseInt(etAno.getText().toString().trim());
        double nota = Double.parseDouble(etNota.getText().toString().trim().replace(",", "."));
        boolean assistido = cbAssistido.isChecked();
        long timestamp = System.currentTimeMillis();

        if (editingMediaId == null) {
            MediaItem novoItem = new MediaItem(nome, tipo, genero, ano, nota, assistido, timestamp);

            if (db != null) {
                db.collection("filmes_series")
                        .add(novoItem)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(MainActivity.this, R.string.msg_sucesso_salvar, Toast.LENGTH_SHORT).show();
                            limparFormulario();
                            esconderTeclado();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, getString(R.string.msg_erro_firebase) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                novoItem.setId(String.valueOf(System.currentTimeMillis()));
                allMediaList.add(0, novoItem);
                aplicarFiltrosEOrdenacao();
                Toast.makeText(this, R.string.msg_sucesso_salvar, Toast.LENGTH_SHORT).show();
                limparFormulario();
                esconderTeclado();
            }
        } else {
            MediaItem itemAtualizado = new MediaItem(editingMediaId, nome, tipo, genero, ano, nota, assistido, timestamp);

            if (db != null) {
                db.collection("filmes_series")
                        .document(editingMediaId)
                        .set(itemAtualizado)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, R.string.msg_sucesso_atualizar, Toast.LENGTH_SHORT).show();
                            limparFormulario();
                            esconderTeclado();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, getString(R.string.msg_erro_firebase) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                for (int i = 0; i < allMediaList.size(); i++) {
                    if (allMediaList.get(i).getId().equals(editingMediaId)) {
                        allMediaList.set(i, itemAtualizado);
                        break;
                    }
                }
                aplicarFiltrosEOrdenacao();
                Toast.makeText(this, R.string.msg_sucesso_atualizar, Toast.LENGTH_SHORT).show();
                limparFormulario();
                esconderTeclado();
            }
        }
    }

    private boolean validarFormulario() {
        tilNome.setError(null);
        tilAno.setError(null);
        tilNota.setError(null);

        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            tilNome.setError(getString(R.string.msg_erro_nome));
            etNome.requestFocus();
            return false;
        }

        if (spTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.msg_erro_tipo, Toast.LENGTH_SHORT).show();
            spTipo.requestFocus();
            return false;
        }

        if (spGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.msg_erro_genero, Toast.LENGTH_SHORT).show();
            spGenero.requestFocus();
            return false;
        }

        String anoStr = etAno.getText() != null ? etAno.getText().toString().trim() : "";
        if (anoStr.isEmpty()) {
            tilAno.setError(getString(R.string.msg_erro_ano));
            etAno.requestFocus();
            return false;
        }
        try {
            int ano = Integer.parseInt(anoStr);
            if (ano < 1888 || ano > 2100) {
                tilAno.setError(getString(R.string.msg_erro_ano));
                etAno.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            tilAno.setError(getString(R.string.msg_erro_ano));
            etAno.requestFocus();
            return false;
        }

        String notaStr = etNota.getText() != null ? etNota.getText().toString().trim().replace(",", ".") : "";
        if (notaStr.isEmpty()) {
            tilNota.setError(getString(R.string.msg_erro_nota));
            etNota.requestFocus();
            return false;
        }
        try {
            double nota = Double.parseDouble(notaStr);
            if (nota < 0.0 || nota > 10.0) {
                tilNota.setError(getString(R.string.msg_erro_nota));
                etNota.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            tilNota.setError(getString(R.string.msg_erro_nota));
            etNota.requestFocus();
            return false;
        }

        return true;
    }

    private void aplicarFiltrosEOrdenacao() {
        filteredMediaList.clear();

        String termoBusca = etBusca.getText() != null ? etBusca.getText().toString().trim().toLowerCase(Locale.getDefault()) : "";
        String tipoFiltro = spFiltroTipo.getSelectedItem() != null ? spFiltroTipo.getSelectedItem().toString() : "Todos os Tipos";
        boolean apenasAssistidos = cbFiltroAssistidos.isChecked();
        int ordenacaoPos = spOrdenacao.getSelectedItemPosition();

        for (MediaItem item : allMediaList) {
            boolean bateBusca = termoBusca.isEmpty()
                    || item.getNome().toLowerCase(Locale.getDefault()).contains(termoBusca)
                    || item.getGenero().toLowerCase(Locale.getDefault()).contains(termoBusca);

            boolean bateTipo = tipoFiltro.equals("Todos os Tipos") || item.getTipo().equalsIgnoreCase(tipoFiltro);

            boolean bateAssistido = !apenasAssistidos || item.isAssistido();

            if (bateBusca && bateTipo && bateAssistido) {
                filteredMediaList.add(item);
            }
        }

        switch (ordenacaoPos) {
            case 1:
                Collections.sort(filteredMediaList, (o1, o2) -> Double.compare(o2.getNota(), o1.getNota()));
                break;
            case 2:
                Collections.sort(filteredMediaList, (o1, o2) -> Double.compare(o1.getNota(), o2.getNota()));
                break;
            case 3:
                Collections.sort(filteredMediaList, (o1, o2) -> Integer.compare(o2.getAno(), o1.getAno()));
                break;
            case 4:
                Collections.sort(filteredMediaList, (o1, o2) -> Integer.compare(o1.getAno(), o2.getAno()));
                break;
            case 5:
                Collections.sort(filteredMediaList, (o1, o2) -> o1.getNome().compareToIgnoreCase(o2.getNome()));
                break;
            default:
                Collections.sort(filteredMediaList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                break;
        }

        adapter.setMediaList(filteredMediaList);

        if (filteredMediaList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvMedias.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvMedias.setVisibility(View.VISIBLE);
        }

        tvListHeader.setText(getString(R.string.header_listagem) + " (" + filteredMediaList.size() + ")");
    }

    @Override
    public void onMediaClick(MediaItem item) {
        editingMediaId = item.getId();
        tvFormTitle.setText(getString(R.string.header_edicao) + ": " + item.getNome());
        btnSalvar.setText(R.string.btn_atualizar);
        btnCancelar.setVisibility(View.VISIBLE);

        etNome.setText(item.getNome());
        etAno.setText(String.valueOf(item.getAno()));
        etNota.setText(String.valueOf(item.getNota()));
        cbAssistido.setChecked(item.isAssistido());

        selecionarSpinnerItem(spTipo, item.getTipo());
        selecionarSpinnerItem(spGenero, item.getGenero());

        if (nestedScrollView != null) {
            nestedScrollView.smoothScrollTo(0, 0);
        }
        etNome.requestFocus();
    }

    @Override
    public void onMediaLongClick(MediaItem item) {
        String mensagem = String.format(getString(R.string.confirm_delete_message), item.getNome());

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(mensagem)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.btn_sim, (dialog, which) -> excluirMedia(item))
                .setNegativeButton(R.string.btn_nao, null)
                .show();
    }

    private void excluirMedia(MediaItem item) {
        if (item == null || item.getId() == null) return;

        if (db != null) {
            db.collection("filmes_series")
                    .document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, R.string.msg_sucesso_excluir, Toast.LENGTH_SHORT).show();
                        if (item.getId().equals(editingMediaId)) {
                            limparFormulario();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, getString(R.string.msg_erro_firebase) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            allMediaList.remove(item);
            aplicarFiltrosEOrdenacao();
            Toast.makeText(this, R.string.msg_sucesso_excluir, Toast.LENGTH_SHORT).show();
            if (item.getId().equals(editingMediaId)) {
                limparFormulario();
            }
        }
    }

    private void limparFormulario() {
        editingMediaId = null;
        tvFormTitle.setText(R.string.header_cadastro);
        btnSalvar.setText(R.string.btn_salvar);
        btnCancelar.setVisibility(View.GONE);

        etNome.setText("");
        etAno.setText("");
        etNota.setText("");
        spTipo.setSelection(0);
        spGenero.setSelection(0);
        cbAssistido.setChecked(false);

        tilNome.setError(null);
        tilAno.setError(null);
        tilNota.setError(null);
    }

    private void selecionarSpinnerItem(Spinner spinner, String valor) {
        if (spinner == null || valor == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void esconderTeclado() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}
