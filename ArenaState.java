package com.oyun.randomitem.arena;

public enum ArenaState {
    BEKLEMEDE,  // oyuncu bekleniyor, menuden katilinabilir
    BASLIYOR,   // harita dolu, geri sayim yapiliyor
    OYUNDA,     // oyun devam ediyor, menuden katilinamaz
    BITTI       // oyun yeni bitti, temizlik yapiliyor
}
