package com.example.receipematcher.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.receipematcher.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

public class CookingFragment extends Fragment {
    private ArrayList<String> steps = new ArrayList<>();
    private int index = 0;
    private TextToSpeech tts;
    private String recipeTitle = "Recipe";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cooking, container, false);

        MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Cooking Mode");
            try { toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material); } catch (Exception ignored) {}
            toolbar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());
        }

        TextView stepCounter = v.findViewById(R.id.textStepCounter);
        TextView currentStep = v.findViewById(R.id.textCurrentStep);
        MaterialButton btnPrev = v.findViewById(R.id.btnPrev);
        MaterialButton btnNext = v.findViewById(R.id.btnNext);

        Bundle args = getArguments();
        if (args != null) {
            ArrayList<String> st = args.getStringArrayList("steps");
            if (st != null) steps = st;
            String title = args.getString("title", "Recipe");
            recipeTitle = title;
            if (toolbar != null) toolbar.setTitle(title);
        }

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Runnable render = () -> {
            int total = Math.max(1, steps.size());
            int safeIndex = Math.max(0, Math.min(index, total - 1));
            index = safeIndex;
            stepCounter.setText("Step " + (safeIndex + 1) + "/" + total);
            String text = steps.isEmpty() ? "No steps" : steps.get(safeIndex);
            currentStep.setText(text);
            btnPrev.setEnabled(safeIndex > 0);
            btnNext.setText(safeIndex < total - 1 ? "Next" : "Done");
            speakStep();
        };

        btnPrev.setOnClickListener(view -> { index = Math.max(0, index - 1); render.run(); });
        btnNext.setOnClickListener(view -> {
            // Confirm before proceeding to next step
            int total = Math.max(1, steps.size());
            boolean last = index >= total - 1;
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(last ? "Finish Cooking" : "Proceed to Next Step")
                    .setMessage(last ? "This is the final step. Finish?" : "Can I proceed to the next step?")
                    .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                    .setPositiveButton(last ? "Finish" : "Next", (d, w) -> {
                        if (!last) { index++; render.run(); }
                        else {
                            speakFinal();
                            // Delay briefly to let TTS start, then exit
                            currentStep.postDelayed(() -> requireActivity().onBackPressed(), 1200);
                        }
                    })
                    .show();
        });

        render.run();

        // Initialize TTS
        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                try { tts.setLanguage(Locale.getDefault()); } catch (Exception ignored) {}
                speakStep();
            }
        });
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (tts != null) {
            try { tts.stop(); tts.shutdown(); } catch (Exception ignored) {}
            tts = null;
        }
    }

    private void speakStep() {
        if (tts == null) return;
        int total = Math.max(1, steps.size());
        int stepNum = Math.max(1, Math.min(index + 1, total));
        boolean last = stepNum == total;
        String text = steps.isEmpty() ? "No steps" : steps.get(index);
        String prefix = last ? "Final step. " : ("Step " + stepNum + ". ");
        String utterance = prefix + text;
        try { tts.stop(); } catch (Exception ignored) {}
        try { tts.speak(utterance, TextToSpeech.QUEUE_FLUSH, null, "step_" + stepNum); } catch (Exception ignored) {}
    }

    private void speakFinal() {
        if (tts == null) return;
        String end = "This is the final. Bon appétit.";
        try { tts.speak(end, TextToSpeech.QUEUE_ADD, null, "final"); } catch (Exception ignored) {}
    }
}
