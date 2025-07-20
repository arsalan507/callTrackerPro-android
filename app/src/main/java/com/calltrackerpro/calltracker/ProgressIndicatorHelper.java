package com.calltrackerpro.calltracker;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class ProgressIndicatorHelper {

    public static void updateProgressIndicator(Context context, View rootView, int currentStep, int totalSteps) {
        View step1Circle = rootView.findViewById(R.id.step1Circle);
        View step2Circle = rootView.findViewById(R.id.step2Circle);
        View connectionLine = rootView.findViewById(R.id.connectionLine);
        TextView step1Label = rootView.findViewById(R.id.step1Label);
        TextView step2Label = rootView.findViewById(R.id.step2Label);

        if (currentStep == 1) {
            // Step 1 is active
            step1Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_active));
            step2Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_inactive));

            step1Label.setAlpha(1.0f);
            step2Label.setAlpha(0.5f);

            connectionLine.setAlpha(0.3f);

        } else if (currentStep == 2) {
            // Step 2 is active, Step 1 is completed
            step1Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_completed));
            step2Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_active));

            step1Label.setAlpha(0.8f);
            step2Label.setAlpha(1.0f);

            connectionLine.setAlpha(1.0f);
        }
    }

    public static void animateProgressUpdate(Context context, View rootView, int fromStep, int toStep) {
        // Add smooth transition animation between steps
        View step1Circle = rootView.findViewById(R.id.step1Circle);
        View step2Circle = rootView.findViewById(R.id.step2Circle);
        View connectionLine = rootView.findViewById(R.id.connectionLine);

        if (fromStep == 1 && toStep == 2) {
            // Animate from step 1 to step 2
            step1Circle.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        step1Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_completed));
                        step1Circle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    });

            connectionLine.animate().alpha(1.0f).setDuration(300).start();

            step2Circle.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        step2Circle.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_active));
                        step2Circle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    });
        }
    }
}