package com.example.ac2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ac2.R;
import com.example.ac2.model.MediaItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(MediaItem item);
        void onMediaLongClick(MediaItem item);
    }

    private final Context context;
    private List<MediaItem> mediaList;
    private final OnMediaClickListener listener;

    public MediaAdapter(Context context, List<MediaItem> mediaList, OnMediaClickListener listener) {
        this.context = context;
        this.mediaList = mediaList != null ? mediaList : new ArrayList<>();
        this.listener = listener;
    }

    public void setMediaList(List<MediaItem> newList) {
        this.mediaList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardMedia;
        private final TextView tvNome;
        private final TextView tvNota;
        private final TextView tvTipo;
        private final TextView tvGenero;
        private final TextView tvAno;
        private final TextView tvAssistidoStatus;
        private final TextView btnEditarItem;
        private final TextView btnExcluirItem;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMedia = itemView.findViewById(R.id.cardMedia);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvNota = itemView.findViewById(R.id.tvNota);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvGenero = itemView.findViewById(R.id.tvGenero);
            tvAno = itemView.findViewById(R.id.tvAno);
            tvAssistidoStatus = itemView.findViewById(R.id.tvAssistidoStatus);
            btnEditarItem = itemView.findViewById(R.id.btnEditarItem);
            btnExcluirItem = itemView.findViewById(R.id.btnExcluirItem);
        }

        public void bind(MediaItem item) {
            tvNome.setText(item.getNome());
            tvNota.setText(String.format(Locale.getDefault(), "★ %.1f", item.getNota()));
            tvTipo.setText(item.getTipo());
            tvGenero.setText(item.getGenero());
            tvAno.setText("Lançamento: " + item.getAno());

            if (item.isAssistido()) {
                tvAssistidoStatus.setText("✓ Assistido");
                tvAssistidoStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chip_green));
            } else {
                tvAssistidoStatus.setText("⏳ Para Assistir");
                tvAssistidoStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chip_orange));
            }

            cardMedia.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaClick(item);
                }
            });

            btnEditarItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaClick(item);
                }
            });

            cardMedia.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMediaLongClick(item);
                }
                return true;
            });

            btnExcluirItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaLongClick(item);
                }
            });
        }
    }
}
