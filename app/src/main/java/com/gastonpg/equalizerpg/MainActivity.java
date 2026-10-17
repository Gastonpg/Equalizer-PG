package com.gastonpg.equalizerpg;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(9, 11, 18);
    private final int panel = Color.rgb(20, 23, 35);
    private final int panel2 = Color.rgb(27, 30, 45);
    private final int accent = Color.rgb(158, 143, 255);
    private final int muted = Color.rgb(151, 158, 180);
    private boolean enabled = true;
    private String selectedProfile = "Mi sonido";
    private LinearLayout root;
    private TextView powerButton;
    private TextView activeName;
    private LinearLayout profileList;
    private String activeCategory = "Para vos";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildHome();
    }

    private void buildHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(26));
        scroll.addView(page);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView logo = text("PG", 16, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(shape(accent, 16));
        header.addView(logo, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout titleStack = new LinearLayout(this);
        titleStack.setOrientation(LinearLayout.VERTICAL);
        titleStack.setPadding(dp(12), 0, 0, 0);
        titleStack.addView(text("EQUALIZER PG", 14, Color.WHITE, true));
        TextView subtitle = text("Tu sonido, a tu manera", 12, muted, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-2, -2);
        subLp.topMargin = dp(3);
        titleStack.addView(subtitle, subLp);
        header.addView(titleStack, new LinearLayout.LayoutParams(0, -2, 1));
        TextView settings = text("⚙", 22, Color.WHITE, false);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(shape(panel2, 16));
        header.addView(settings, new LinearLayout.LayoutParams(dp(44), dp(44)));
        settings.setOnClickListener(v -> Toast.makeText(this, "Ajustes: próximamente", Toast.LENGTH_SHORT).show());
        page.addView(header);

        LinearLayout hero = card();
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.topMargin = dp(25);
        page.addView(hero, heroLp);

        LinearLayout heroTop = new LinearLayout(this);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);
        heroTop.setOrientation(LinearLayout.HORIZONTAL);
        TextView tag = text("●  PROCESAMIENTO DE AUDIO", 10, accent, true);
        heroTop.addView(tag, new LinearLayout.LayoutParams(0, -2, 1));
        powerButton = text(enabled ? "ON" : "OFF", 11, enabled ? Color.BLACK : muted, true);
        powerButton.setGravity(Gravity.CENTER);
        powerButton.setBackground(shape(enabled ? accent : panel2, 18));
        heroTop.addView(powerButton, new LinearLayout.LayoutParams(dp(54), dp(32)));
        powerButton.setOnClickListener(v -> {
            enabled = !enabled;
            powerButton.setText(enabled ? "ON" : "OFF");
            powerButton.setTextColor(enabled ? Color.BLACK : muted);
            powerButton.setBackground(shape(enabled ? accent : panel2, 18));
            Toast.makeText(this, enabled ? "Ecualizador activado (vista previa)" : "Ecualizador desactivado", Toast.LENGTH_SHORT).show();
        });
        hero.addView(heroTop);

        TextView heroTitle = text("Dale forma\na tu sonido.", 30, Color.WHITE, true);
        heroTitle.setLineSpacing(dp(1), 1f);
        LinearLayout.LayoutParams heroTitleLp = new LinearLayout.LayoutParams(-1, -2);
        heroTitleLp.topMargin = dp(20);
        hero.addView(heroTitle, heroTitleLp);
        TextView heroDesc = text("Perfiles diseñados para sentir cada detalle.", 13, muted, false);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(-1, -2);
        descLp.topMargin = dp(9);
        hero.addView(heroDesc, descLp);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(48, 49, 68));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(-1, dp(1));
        divLp.topMargin = dp(19);
        divLp.bottomMargin = dp(15);
        hero.addView(divider, divLp);

        LinearLayout current = new LinearLayout(this);
        current.setGravity(Gravity.CENTER_VERTICAL);
        current.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout now = new LinearLayout(this);
        now.setOrientation(LinearLayout.VERTICAL);
        now.addView(text("PERFIL ACTIVO", 10, muted, true));
        activeName = text(selectedProfile, 17, Color.WHITE, true);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(-2, -2);
        nameLp.topMargin = dp(5);
        now.addView(activeName, nameLp);
        current.addView(now, new LinearLayout.LayoutParams(0, -2, 1));
        TextView edit = text("Editar  ↗", 12, accent, true);
        edit.setPadding(dp(12), dp(10), dp(12), dp(10));
        edit.setBackground(shape(panel2, 14));
        current.addView(edit);
        edit.setOnClickListener(v -> showEditor());
        hero.addView(current);

        TextView categoryLabel = text("Explorar perfiles", 20, Color.WHITE, true);
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(-1, -2);
        catLp.topMargin = dp(28);
        page.addView(categoryLabel, catLp);
        page.addView(text("Elegí un punto de partida para tu música.", 12, muted, false));

        HorizontalScrollView tabsScroll = new HorizontalScrollView(this);
        tabsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] categories = {"Para vos", "Géneros", "Carácter", "Aiwa + Genezi", "Mis perfiles"};
        for (String c : categories) {
            TextView tab = text(c, 12, c.equals(activeCategory) ? Color.BLACK : muted, true);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(15), dp(11), dp(15), dp(11));
            tab.setBackground(shape(c.equals(activeCategory) ? accent : panel, 20));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-2, dp(40));
            tlp.rightMargin = dp(8);
            tabs.addView(tab, tlp);
            tab.setOnClickListener(v -> {
                activeCategory = c;
                buildHome();
                root.post(() -> {});
            });
        }
        tabsScroll.addView(tabs);
        LinearLayout.LayoutParams tabsLp = new LinearLayout.LayoutParams(-1, dp(42));
        tabsLp.topMargin = dp(16);
        page.addView(tabsScroll, tabsLp);

        profileList = new LinearLayout(this);
        profileList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(-1, -2);
        listLp.topMargin = dp(13);
        page.addView(profileList, listLp);
        populateProfiles();

        TextView eqTitle = text("Ecualizador", 20, Color.WHITE, true);
        LinearLayout.LayoutParams eqTitleLp = new LinearLayout.LayoutParams(-1, -2);
        eqTitleLp.topMargin = dp(27);
        page.addView(eqTitle, eqTitleLp);
        page.addView(text("Vista previa de las bandas • −12 dB a +12 dB", 12, muted, false));
        LinearLayout eqCard = card();
        eqCard.setPadding(dp(12), dp(16), dp(12), dp(14));
        LinearLayout.LayoutParams eqLp = new LinearLayout.LayoutParams(-1, -2);
        eqLp.topMargin = dp(13);
        page.addView(eqCard, eqLp);

        LinearLayout bands = new LinearLayout(this);
        bands.setGravity(Gravity.CENTER);
        bands.setOrientation(LinearLayout.HORIZONTAL);
        int[] values = {-2, 1, 1, -1, 0, 0, 1, 1, 1, 0};
        String[] freqs = {"31", "63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"};
        for (int i = 0; i < freqs.length; i++) {
            LinearLayout band = new LinearLayout(this);
            band.setOrientation(LinearLayout.VERTICAL);
            band.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView val = text((values[i] > 0 ? "+" : "") + values[i], 9, muted, false);
            val.setGravity(Gravity.CENTER);
            band.addView(val, new LinearLayout.LayoutParams(-1, dp(17)));
            SeekBar seek = new SeekBar(this);
            seek.setMax(24);
            seek.setProgress(values[i] + 12);
            seek.setRotation(-90f);
            seek.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));
            seek.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            seek.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(60, 62, 81)));
            LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(dp(76), dp(32));
            seekLp.topMargin = dp(34);
            seekLp.bottomMargin = dp(34);
            band.addView(seek, seekLp);
            TextView freq = text(freqs[i], 9, muted, false);
            freq.setGravity(Gravity.CENTER);
            band.addView(freq, new LinearLayout.LayoutParams(-1, dp(20)));
            LinearLayout.LayoutParams bandLp = new LinearLayout.LayoutParams(0, dp(140), 1);
            bands.addView(band, bandLp);
        }
        eqCard.addView(bands);
        TextView preamp = text("PREAMP", 10, muted, true);
        LinearLayout.LayoutParams preLp = new LinearLayout.LayoutParams(-1, -2);
        preLp.topMargin = dp(15);
        eqCard.addView(preamp, preLp);
        SeekBar pre = new SeekBar(this);
        pre.setMax(24);
        pre.setProgress(10);
        pre.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));
        pre.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        eqCard.addView(pre, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setBackground(shape(panel, 22));
        footer.setPadding(dp(12), dp(8), dp(12), dp(8));
        TextView home = text("⌂  Inicio", 12, accent, true);
        TextView profiles = text("♫  Perfiles", 12, muted, true);
        TextView personal = text("✦  Guardados", 12, muted, true);
        home.setGravity(Gravity.CENTER);
        profiles.setGravity(Gravity.CENTER);
        personal.setGravity(Gravity.CENTER);
        footer.addView(home, new LinearLayout.LayoutParams(0, dp(36), 1));
        footer.addView(profiles, new LinearLayout.LayoutParams(0, dp(36), 1));
        footer.addView(personal, new LinearLayout.LayoutParams(0, dp(36), 1));
        profiles.setOnClickListener(v -> Toast.makeText(this, "Perfiles disponibles arriba", Toast.LENGTH_SHORT).show());
        personal.setOnClickListener(v -> Toast.makeText(this, "Tus perfiles guardados aparecerán aquí", Toast.LENGTH_SHORT).show());
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams footerLp = (LinearLayout.LayoutParams) footer.getLayoutParams();
        footerLp.setMargins(dp(16), dp(6), dp(16), dp(10));
        footer.setLayoutParams(footerLp);

        setContentView(root);
    }

    private void populateProfiles() {
        profileList.removeAllViews();
        String[][] data;
        switch (activeCategory) {
            case "Géneros":
                data = new String[][]{{"Trap / Hip-Hop","Subgrave con pegada y voces presentes","♬"},{"RKT","Energía y golpe para el ritmo","↗"},{"Cumbia santafesina","Cuerpo, claridad y acordeón al frente","♫"},{"Reggaeton","Graves firmes y brillo suave","◉"},{"Chamamé","Instrumentos acústicos con naturalidad","⌁"},{"Rock","Guitarras definidas y batería presente","⚡"},{"Pop","Balance moderno y voces claras","✧"},{"Electrónica","Impacto y detalle en los agudos","◈"}};
                break;
            case "Carácter":
                data = new String[][]{{"Subgrave","Más profundidad, con control","↓"},{"Punchy bass","Golpe rápido sin exceso de cola","↗"},{"Controlled bass","Graves firmes y ordenados","◉"},{"Warm","Sonido cálido y relajado","☼"},{"Voces al frente","Más presencia para las voces","◖"},{"Máxima claridad","Detalle sin exagerar los agudos","✧"},{"V-Shape","Graves y agudos destacados","⌁"},{"Balanced","Equilibrio para escuchar de todo","◈"}};
                break;
            case "Aiwa + Genezi":
                data = new String[][]{{"T-BASS intermedio","Inspirado en el ajuste entre nivel 1 y 2","〰"},{"Anti-Boom","Control de graves profundos","↓"},{"Genezi Power","Impacto para escuchar fuerte","⚡"},{"Bajo definido","Grave firme sin retumbe","◉"},{"Hi-Fi","Balance y detalle","✧"},{"Loudness","Cuerpo a volumen bajo","↗"},{"Máxima claridad","Voces e instrumentos más definidos","◖"},{"Fiesta Genezi","Perfil enérgico para reuniones","♫"}};
                break;
            case "Mis perfiles":
                data = new String[][]{{"Mi sonido","Tu perfil personal principal","✦"},{"Sonido de papá","Perfil familiar guardado","♡"},{"Favoritos","Acceso rápido a tus perfiles","☆"},{"Crear perfil","Empezar un ajuste personalizado","＋"}};
                break;
            default:
                data = new String[][]{{"Mi sonido","Tu perfil personal principal","✦"},{"Trap / Hip-Hop","Subgrave con pegada y voces presentes","♬"},{"Cumbia santafesina","Cuerpo, claridad y acordeón al frente","♫"},{"Máxima claridad","Detalle sin exagerar los agudos","✧"},{"T-BASS intermedio","Graves con cuerpo, sin retumbe excesivo","〰"}};
        }
        for (String[] item : data) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(13), dp(12), dp(12), dp(12));
            row.setBackground(shape(panel, 18));
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
            rowLp.bottomMargin = dp(8);
            profileList.addView(row, rowLp);
            TextView icon = text(item[2], 18, accent, true);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(shape(panel2, 14));
            row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12), 0, dp(5), 0);
            info.addView(text(item[0], 14, Color.WHITE, true));
            TextView desc = text(item[1], 11, muted, false);
            LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(-1, -2);
            descLp.topMargin = dp(4);
            info.addView(desc, descLp);
            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            TextView choose = text(item[0].equals(selectedProfile) ? "✓" : "›", 19, item[0].equals(selectedProfile) ? accent : muted, true);
            row.addView(choose, new LinearLayout.LayoutParams(dp(24), -2));
            row.setOnClickListener(v -> {
                if (item[0].equals("Crear perfil")) {
                    showEditor();
                    return;
                }
                selectedProfile = item[0];
                activeName.setText(selectedProfile);
                Toast.makeText(this, selectedProfile + " seleccionado (vista previa)", Toast.LENGTH_SHORT).show();
                buildHome();
            });
        }
    }

    private void showEditor() {
        Toast.makeText(this, "Editor de perfiles y guardado: próxima etapa", Toast.LENGTH_LONG).show();
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(shape(panel, 24));
        return c;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        else t.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        return t;
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.rgb(43, 45, 63));
        return d;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
