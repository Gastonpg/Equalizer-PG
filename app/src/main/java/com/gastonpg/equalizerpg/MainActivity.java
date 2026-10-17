package com.gastonpg.equalizerpg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.media.audiofx.Equalizer;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(8, 9, 19);
    private final int panel = Color.rgb(21, 23, 43);
    private final int panel2 = Color.rgb(29, 31, 57);
    private final int accent = Color.rgb(168, 132, 255);
    private final int cyan = Color.rgb(81, 224, 255);
    private final int pink = Color.rgb(255, 104, 195);
    private final int muted = Color.rgb(163, 169, 195);
    private boolean enabled = true;
    private String selectedProfile = "Mi sonido";
    private LinearLayout root;
    private SynthwaveBackground synthwaveBackground;
    private TextView powerButton;
    private TextView activeName;
    private LinearLayout profileList;
    private String activeCategory = "Para vos";
    private String visualizerStyle = "Liquid Orb";
    private Equalizer systemEqualizer;
    private short nativeMinLevel = -1200;
    private short nativeMaxLevel = 1200;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        visualizerStyle = getPreferences(MODE_PRIVATE).getString("visualizer_style", "Liquid Orb");
        initializeAudioEngine();
        buildHome();
    }

    private void buildHome() {
        FrameLayout screen = new FrameLayout(this);
        synthwaveBackground = new SynthwaveBackground();
        screen.addView(synthwaveBackground, new FrameLayout.LayoutParams(-1, -1));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.setClipToPadding(false);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(26));
        scroll.addView(page);
        scroll.setOnScrollChangeListener((View v, int sx, int sy, int oldSx, int oldSy) ->
                synthwaveBackground.setScrollOffset(sy));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        HeadphoneLogo logo = new HeadphoneLogo();
        header.addView(logo, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout titleStack = new LinearLayout(this);
        titleStack.setOrientation(LinearLayout.VERTICAL);
        titleStack.setPadding(dp(12), 0, 0, 0);
        titleStack.addView(text("EQUALIZER PG", 14, Color.WHITE, true));
        TextView subtitle = text("PG Pods  •  Tu sonido, a tu manera", 11, muted, false);
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
        hero.setBackground(glassGradient(26));
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.topMargin = dp(25);
        page.addView(hero, heroLp);

        LinearLayout heroTop = new LinearLayout(this);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);
        heroTop.setOrientation(LinearLayout.HORIZONTAL);
        TextView tag = text("●  ECUALIZADOR DEL SISTEMA", 10, accent, true);
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
            if (enabled) {
                startAudioEngine(AudioEngineService.ACTION_START, -1, 0);
                Toast.makeText(this, "Intentando activar el ecualizador del sistema", Toast.LENGTH_SHORT).show();
            } else {
                startAudioEngine(AudioEngineService.ACTION_STOP, -1, 0);
                Toast.makeText(this, "Ecualizador desactivado", Toast.LENGTH_SHORT).show();
            }
        });
        hero.addView(heroTop);

        LinearLayout heroVisual = new LinearLayout(this);
        heroVisual.setGravity(Gravity.CENTER_VERTICAL);
        heroVisual.setOrientation(LinearLayout.HORIZONTAL);
        TextView heroTitle = text("Dale forma\na tu sonido.", 29, Color.WHITE, true);
        heroTitle.setLineSpacing(dp(1), 1f);
        heroVisual.addView(heroTitle, new LinearLayout.LayoutParams(0, -2, 1));
        WaveOrb orb = new WaveOrb();
        orb.setOnClickListener(v -> showVisualizerPicker(orb));
        orb.setContentDescription("Elegir estilo de visualizador: " + visualizerStyle);
        LinearLayout.LayoutParams orbLp = new LinearLayout.LayoutParams(dp(104), dp(104));
        orbLp.leftMargin = dp(4);
        heroVisual.addView(orb, orbLp);
        LinearLayout.LayoutParams heroTitleLp = new LinearLayout.LayoutParams(-1, -2);
        heroTitleLp.topMargin = dp(13);
        hero.addView(heroVisual, heroTitleLp);
        TextView heroDesc = text("Prueba real del ecualizador de Android. El efecto global depende del dispositivo.", 13, muted, false);
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
        page.addView(text("Bandas de audio reales • −12 dB a +12 dB", 12, muted, false));
        LinearLayout eqCard = card();
        eqCard.setBackground(glassGradient(22));
        eqCard.setPadding(dp(12), dp(16), dp(12), dp(14));
        LinearLayout.LayoutParams eqLp = new LinearLayout.LayoutParams(-1, -2);
        eqLp.topMargin = dp(13);
        page.addView(eqCard, eqLp);

        LinearLayout bands = new LinearLayout(this);
        bands.setGravity(Gravity.CENTER);
        bands.setOrientation(LinearLayout.HORIZONTAL);
        if (systemEqualizer != null) {
            short bandCount = systemEqualizer.getNumberOfBands();
            nativeMinLevel = systemEqualizer.getBandLevelRange()[0];
            nativeMaxLevel = systemEqualizer.getBandLevelRange()[1];
            for (short i = 0; i < bandCount; i++) {
                final short bandId = i;
                int centerHz = systemEqualizer.getCenterFreq(i) / 1000;
                String freqLabel = centerHz >= 1000 ? (centerHz % 1000 == 0 ? (centerHz / 1000) + "k" : String.format(java.util.Locale.US, "%.1fk", centerHz / 1000f)) : String.valueOf(centerHz);
                LinearLayout band = new LinearLayout(this);
                band.setOrientation(LinearLayout.VERTICAL);
                band.setGravity(Gravity.CENTER_HORIZONTAL);
                short currentLevel = systemEqualizer.getBandLevel(i);
                TextView val = text(String.format(java.util.Locale.US, "%+.1f", currentLevel / 100f), 9, muted, false);
                val.setGravity(Gravity.CENTER);
                band.addView(val, new LinearLayout.LayoutParams(-1, dp(17)));
                SeekBar seek = new SeekBar(this);
                seek.setMax(nativeMaxLevel - nativeMinLevel);
                seek.setProgress(currentLevel - nativeMinLevel);
                seek.setRotation(-90f);
                seek.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));
                seek.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
                seek.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(60, 62, 81)));
                LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(dp(76), dp(32));
                seekLp.topMargin = dp(34);
                seekLp.bottomMargin = dp(34);
                band.addView(seek, seekLp);
                TextView freq = text(freqLabel, 9, muted, false);
                freq.setGravity(Gravity.CENTER);
                band.addView(freq, new LinearLayout.LayoutParams(-1, dp(20)));
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                        short level = (short) (nativeMinLevel + progress);
                        val.setText(String.format(java.util.Locale.US, "%+.1f", level / 100f));
                        if (fromUser && systemEqualizer != null) {
                            try { systemEqualizer.setBandLevel(bandId, level); }
                            catch (Exception e) { Toast.makeText(MainActivity.this, "El sistema rechazó este ajuste", Toast.LENGTH_SHORT).show(); }
                        }
                    }
                    @Override public void onStartTrackingTouch(SeekBar bar) {}
                    @Override public void onStopTrackingTouch(SeekBar bar) {}
                });
                LinearLayout.LayoutParams bandLp = new LinearLayout.LayoutParams(0, dp(140), 1);
                bands.addView(band, bandLp);
            }
        } else {
            TextView unavailable = text("El dispositivo no expuso un ecualizador global compatible. Revisá el estado del motor.", 12, muted, false);
            bands.addView(unavailable);
        }
        eqCard.addView(bands);
        TextView preampNote = text("Preamp paramétrico y bandas Q: siguiente etapa del motor DSP propio.", 11, muted, false);
        LinearLayout.LayoutParams preLp = new LinearLayout.LayoutParams(-1, -2);
        preLp.topMargin = dp(12);
        eqCard.addView(preampNote, preLp);

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

        screen.addView(root, new FrameLayout.LayoutParams(-1, -1));
        setContentView(screen);
    }


    private void initializeAudioEngine() {
        try {
            systemEqualizer = new Equalizer(0, 0);
            systemEqualizer.setEnabled(enabled);
            short[] range = systemEqualizer.getBandLevelRange();
            nativeMinLevel = range[0];
            nativeMaxLevel = range[1];
        } catch (Throwable error) {
            systemEqualizer = null;
        }
    }

    private void setAudioEnabled(boolean active) {
        if (systemEqualizer == null) {
            Toast.makeText(this, "Este dispositivo no permite iniciar el ecualizador global", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            systemEqualizer.setEnabled(active);
        } catch (Exception error) {
            Toast.makeText(this, "No se pudo cambiar el estado del ecualizador", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // No liberar el efecto aquí: el siguiente paso es mover su ciclo de vida a un servicio.
        super.onDestroy();
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
            row.setBackground(glassGradient(18));
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
            rowLp.bottomMargin = dp(8);
            profileList.addView(row, rowLp);
            TextView icon = text(item[2], 18, accent, true);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(colorGlowShape(14));
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

    private void startAudioEngine(String action, int index, int level) {
        Intent intent = new Intent(this, AudioEngineService.class);
        intent.setAction(action);
        if (index >= 0) intent.putExtra(AudioEngineService.EXTRA_INDEX, index);
        intent.putExtra(AudioEngineService.EXTRA_LEVEL, level);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
        } catch (RuntimeException error) {
            Toast.makeText(this, "Android no permitió iniciar el motor de audio", Toast.LENGTH_LONG).show();
        }
    }

    private void showEditor() {
        Toast.makeText(this, "Editor de perfiles y guardado: próxima etapa", Toast.LENGTH_LONG).show();
    }

    private void showVisualizerPicker(WaveOrb orb) {
        String[] styles = {"Liquid Orb", "Spectrum Bars", "Waveform", "Pulse", "Neon Spectrum", "Liquid Rings"};
        String[] descriptions = {"Círculo líquido multicolor (predeterminado)", "Barras de espectro en movimiento", "Onda horizontal animada", "Pulso suave que se expande", "Barras neón con degradado", "Anillos líquidos concéntricos"};
        new AlertDialog.Builder(this)
                .setTitle("Estilo de visualizador")
                .setSingleChoiceItems(styles, java.util.Arrays.asList(styles).indexOf(visualizerStyle), (dialog, which) -> {
                    visualizerStyle = styles[which];
                    getPreferences(MODE_PRIVATE).edit().putString("visualizer_style", visualizerStyle).apply();
                    orb.invalidate();
                    orb.setContentDescription("Elegir estilo de visualizador: " + visualizerStyle);
                    Toast.makeText(this, descriptions[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(glassGradient(24));
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
        d.setStroke(dp(1), Color.argb(90, 151, 158, 205));
        return d;
    }

    private GradientDrawable glassGradient(int radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(224, 43, 37, 76), Color.argb(222, 24, 31, 58), Color.argb(230, 14, 17, 34)});
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.argb(105, 157, 164, 235));
        return d;
    }

    private GradientDrawable colorGlowShape(int radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(80, 63, 137), Color.rgb(33, 67, 94), Color.rgb(48, 37, 76)});
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.argb(130, 111, 209, 255));
        return d;
    }


    /**
     * Original OutRun-inspired landscape, drawn locally so it works offline.
     * A restrained parallax shift gives the glass UI a foreground/depth feel.
     */
    private class SynthwaveBackground extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float scrollOffset = 0f;

        SynthwaveBackground() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setScrollOffset(int value) {
            scrollOffset = value;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            // Deep navy-to-violet night sky.
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{Color.rgb(9, 10, 27), Color.rgb(29, 13, 54),
                            Color.rgb(61, 19, 79), Color.rgb(10, 13, 30)},
                    new float[]{0f, .43f, .67f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // Sparse stars, intentionally subtle.
            p.setColor(Color.argb(125, 177, 208, 255));
            for (int i = 0; i < 34; i++) {
                float x = ((i * 97f) % Math.max(1f, w));
                float y = ((i * 53f) % Math.max(1f, h * .43f));
                canvas.drawCircle(x, y, (i % 4 == 0) ? dp(1) : dp(1), p);
            }

            // Magenta sun with thin horizontal cut-outs.
            float sunX = w * .76f;
            float sunY = h * .36f - scrollOffset * .035f;
            float sunR = Math.min(w, h) * .19f;
            p.setShader(new LinearGradient(sunX, sunY - sunR, sunX, sunY + sunR,
                    new int[]{Color.rgb(255, 104, 195), Color.rgb(176, 80, 237),
                            Color.rgb(92, 82, 224)}, null, Shader.TileMode.CLAMP));
            canvas.drawCircle(sunX, sunY, sunR, p);
            p.setShader(null);
            p.setColor(Color.rgb(31, 15, 53));
            for (int i = 0; i < 7; i++) {
                float yy = sunY + sunR * .22f + i * dp(4);
                float hh = (i < 3) ? dp(2) : dp(3);
                canvas.drawRect(sunX - sunR, yy, sunX + sunR, yy + hh, p);
            }

            // Layered angular mountains: distant cyan-violet ridge, then dark foreground ridge.
            float mountainShift = scrollOffset * .055f;
            p.setShader(new LinearGradient(0, h * .38f, 0, h * .73f,
                    new int[]{Color.rgb(63, 53, 130), Color.rgb(18, 26, 62)},
                    null, Shader.TileMode.CLAMP));
            android.graphics.Path distant = new android.graphics.Path();
            distant.moveTo(0, h * .61f + mountainShift);
            distant.lineTo(w * .10f, h * .49f + mountainShift);
            distant.lineTo(w * .19f, h * .54f + mountainShift);
            distant.lineTo(w * .34f, h * .40f + mountainShift);
            distant.lineTo(w * .48f, h * .56f + mountainShift);
            distant.lineTo(w * .62f, h * .46f + mountainShift);
            distant.lineTo(w * .76f, h * .58f + mountainShift);
            distant.lineTo(w * .89f, h * .47f + mountainShift);
            distant.lineTo(w, h * .55f + mountainShift);
            distant.lineTo(w, h);
            distant.lineTo(0, h);
            distant.close();
            canvas.drawPath(distant, p);
            p.setShader(null);

            p.setShader(new LinearGradient(0, h * .54f, 0, h,
                    new int[]{Color.rgb(26, 18, 59), Color.rgb(7, 11, 27)},
                    null, Shader.TileMode.CLAMP));
            android.graphics.Path near = new android.graphics.Path();
            near.moveTo(0, h * .68f - mountainShift * .4f);
            near.lineTo(w * .16f, h * .59f - mountainShift * .4f);
            near.lineTo(w * .29f, h * .68f - mountainShift * .4f);
            near.lineTo(w * .43f, h * .56f - mountainShift * .4f);
            near.lineTo(w * .60f, h * .67f - mountainShift * .4f);
            near.lineTo(w * .78f, h * .57f - mountainShift * .4f);
            near.lineTo(w, h * .66f - mountainShift * .4f);
            near.lineTo(w, h);
            near.lineTo(0, h);
            near.close();
            canvas.drawPath(near, p);
            p.setShader(null);

            // Horizon glow, softened so text/cards remain readable.
            p.setShader(new LinearGradient(0, h * .56f, 0, h * .76f,
                    new int[]{Color.argb(75, 255, 92, 210), Color.argb(0, 81, 224, 255)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, h * .55f, w, h * .78f, p);
            p.setShader(null);

            // Perspective neon grid, fading into the horizon.
            float horizon = h * .68f - scrollOffset * .025f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(110, 81, 224, 255));
            for (int i = 0; i < 11; i++) {
                float t = i / 10f;
                float y = horizon + (h - horizon) * t * t;
                canvas.drawLine(0, y, w, y, p);
            }
            for (int i = -8; i <= 8; i++) {
                float bottomX = w * .5f + i * w * .105f;
                canvas.drawLine(w * .5f, horizon, bottomX, h, p);
            }
            p.setStyle(Paint.Style.FILL);

            // Dark vignette overlay around the content edges for readable Liquid Glass panels.
            p.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{Color.argb(35, 3, 5, 17), Color.argb(20, 5, 6, 19),
                            Color.argb(112, 3, 5, 17)}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);
        }
    }

    private class HeadphoneLogo extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        HeadphoneLogo() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            p.setShader(new LinearGradient(0, 0, w, h, new int[]{accent, pink, cyan}, null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(1, 1, w - 1, h - 1), dp(17), dp(17), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor(Color.argb(210, 245, 243, 255));
            RectF arc = new RectF(w * .24f, h * .12f, w * .76f, h * .72f);
            canvas.drawArc(arc, 190, 160, false, p);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(w * .18f, h * .43f, w * .31f, h * .72f, dp(4), dp(4), p);
            canvas.drawRoundRect(w * .69f, h * .43f, w * .82f, h * .72f, dp(4), dp(4), p);
            p.setColor(Color.WHITE);
            p.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
            p.setTextSize(dp(15));
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("PG", w / 2f, h * .64f, p);
        }
    }

    private class WaveOrb extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler();
        private float phase = 0f;
        private final Runnable animator = new Runnable() {
            @Override public void run() {
                phase += .12f;
                if (phase > 1000f) phase = 0f;
                invalidate();
                handler.postDelayed(this, 45);
            }
        };
        WaveOrb() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            handler.post(animator);
        }
        @Override protected void onDetachedFromWindow() {
            handler.removeCallbacks(animator);
            super.onDetachedFromWindow();
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float r = Math.min(cx, cy) * .82f;
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            switch (visualizerStyle) {
                case "Spectrum Bars": drawBars(canvas, cx, cy, r, false); break;
                case "Waveform": drawWaveform(canvas, cx, cy, r); break;
                case "Pulse": drawPulse(canvas, cx, cy, r); break;
                case "Neon Spectrum": drawBars(canvas, cx, cy, r, true); break;
                case "Liquid Rings": drawRings(canvas, cx, cy, r); break;
                default: drawLiquidOrb(canvas, cx, cy, r); break;
            }
        }

        private void drawLiquidOrb(Canvas canvas, float cx, float cy, float r) {
            p.setShader(new SweepGradient(cx, cy, new int[]{accent, cyan, pink, accent}, null));
            canvas.drawCircle(cx, cy, r * .92f, p);
            p.setShader(null);
            p.setColor(Color.rgb(19, 22, 43));
            canvas.drawCircle(cx, cy, r * .78f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(100, 203, 218, 255));
            canvas.drawCircle(cx, cy, r * .65f, p);
            canvas.drawCircle(cx, cy, r * .49f, p);
            p.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 36; i++) {
                double a = (Math.PI * 2 * i / 36) + phase * .12;
                float pulse = (float)(Math.sin(i * .78 + phase) * .5 + .5);
                float inner = r * (.43f + pulse * .07f), outer = inner + dp(4) + pulse * dp(9);
                float x1 = cx + (float)Math.cos(a) * inner, y1 = cy + (float)Math.sin(a) * inner;
                float x2 = cx + (float)Math.cos(a) * outer, y2 = cy + (float)Math.sin(a) * outer;
                p.setStrokeWidth(dp(2)); p.setStrokeCap(Paint.Cap.ROUND);
                p.setShader(new LinearGradient(x1, y1, x2, y2, new int[]{cyan, pink, accent}, null, Shader.TileMode.CLAMP));
                canvas.drawLine(x1, y1, x2, y2, p);
            }
            p.setShader(null);
            drawCenterLabel(canvas, cx, cy);
        }

        private void drawBars(Canvas canvas, float cx, float cy, float r, boolean neon) {
            p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(225, 17, 20, 42));
            canvas.drawRoundRect(new RectF(cx-r, cy-r*.76f, cx+r, cy+r*.76f), dp(18), dp(18), p);
            int count = 17; float gap = dp(3), bw = (r * 1.55f - gap * (count-1)) / count;
            for (int i=0; i<count; i++) {
                float h = dp(8) + (float)(Math.abs(Math.sin(i*.7 + phase*.7)) * r * (neon ? .85f : .68f));
                float x = cx - (count*bw+(count-1)*gap)/2f + i*(bw+gap);
                p.setShader(new LinearGradient(x, cy-h/2, x, cy+h/2, neon ? new int[]{pink, accent, cyan} : new int[]{cyan, accent, pink}, null, Shader.TileMode.CLAMP));
                canvas.drawRoundRect(new RectF(x, cy-h/2, x+bw, cy+h/2), dp(3), dp(3), p);
            }
            p.setShader(null);
        }

        private void drawWaveform(Canvas canvas, float cx, float cy, float r) {
            p.setColor(Color.argb(220, 16, 20, 42));
            canvas.drawRoundRect(new RectF(cx-r, cy-r*.72f, cx+r, cy+r*.72f), dp(18), dp(18), p);
            android.graphics.Path wave = new android.graphics.Path();
            for (int i=0; i<=70; i++) {
                float x = cx-r*.82f + (2*r*.82f*i/70f);
                float y = cy + (float)Math.sin(i*.32 + phase*.75)*r*.23f + (float)Math.sin(i*.81 - phase*.38)*r*.11f;
                if (i==0) wave.moveTo(x,y); else wave.lineTo(x,y);
            }
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(3)); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
            p.setShader(new LinearGradient(cx-r, cy, cx+r, cy, new int[]{cyan, accent, pink}, null, Shader.TileMode.CLAMP));
            canvas.drawPath(wave,p); p.setShader(null); p.setStyle(Paint.Style.FILL);
        }

        private void drawPulse(Canvas canvas, float cx, float cy, float r) {
            p.setColor(Color.argb(230, 18, 21, 43)); canvas.drawCircle(cx,cy,r*.88f,p);
            p.setStyle(Paint.Style.STROKE);
            for (int i=0;i<4;i++) {
                float rr = r*(.28f + i*.17f) + (float)Math.sin(phase-i)*dp(2);
                p.setStrokeWidth(dp(i==3 ? 2 : 1));
                p.setColor(i%2==0 ? Color.argb(190,81,224,255) : Color.argb(190,255,104,195));
                canvas.drawCircle(cx,cy,rr,p);
            }
            p.setStyle(Paint.Style.FILL); p.setShader(new SweepGradient(cx,cy,new int[]{accent,cyan,pink,accent},null));
            canvas.drawCircle(cx,cy,r*(.19f + (float)Math.sin(phase)*.025f),p); p.setShader(null);
        }

        private void drawRings(Canvas canvas, float cx, float cy, float r) {
            p.setColor(Color.argb(225,17,20,42)); canvas.drawCircle(cx,cy,r*.9f,p);
            p.setStyle(Paint.Style.STROKE);
            for (int i=0;i<5;i++) {
                p.setStrokeWidth(dp(i==0 ? 3 : 2));
                p.setShader(new SweepGradient(cx,cy,new int[]{cyan,accent,pink,cyan}, null));
                float rr = r*(.25f+i*.13f) + (float)Math.sin(phase+i*.7f)*dp(2);
                canvas.drawCircle(cx,cy,rr,p);
            }
            p.setShader(null); p.setStyle(Paint.Style.FILL); drawCenterLabel(canvas,cx,cy);
        }

        private void drawCenterLabel(Canvas canvas, float cx, float cy) {
            p.setShader(null); p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE);
            p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            p.setTextSize(dp(10)); canvas.drawText("PG", cx, cy + dp(4), p);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
