package dft.hushplanes.android;

import android.content.Context;
import android.support.design.widget.*;
import android.util.AttributeSet;
import android.view.View;

public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<View> {

	public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
		child.setTranslationY(translationY);
		return true;
	}

	@Override
	public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
		float translationY = Math.min(0, parent.getBottom() - child.getBottom());
		child.setTranslationY(translationY);
	}
}
