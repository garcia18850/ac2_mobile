package com.example.ac2.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MediaItem {
    @DocumentId
    private String id;
    private String nome;
    private String tipo;
    private String genero;
    private int ano;
    private double nota;
    private boolean assistido;
    private long timestamp;

    public MediaItem() {
    }

    public MediaItem(String nome, String tipo, String genero, int ano, double nota, boolean assistido, long timestamp) {
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.ano = ano;
        this.nota = nota;
        this.assistido = assistido;
        this.timestamp = timestamp;
    }

    public MediaItem(String id, String nome, String tipo, String genero, int ano, double nota, boolean assistido, long timestamp) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.ano = ano;
        this.nota = nota;
        this.assistido = assistido;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public boolean isAssistido() {
        return assistido;
    }

    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
