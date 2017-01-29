package net.iquesoft.iquephoto.ui.fragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import net.iquesoft.iquephoto.R;
import net.iquesoft.iquephoto.core.editor.ImageEditorView;
import net.iquesoft.iquephoto.core.editor.enums.EditorTool;
import net.iquesoft.iquephoto.presentation.common.ToolFragment;
import net.iquesoft.iquephoto.presentation.presenters.fragment.ImageAdjustmentPresenter;
import net.iquesoft.iquephoto.presentation.views.fragment.ImageAdjustmentView;
import net.iquesoft.iquephoto.utils.ToolbarUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ImageAdjustmentFragment extends ToolFragment implements ImageAdjustmentView {
    public static final String ARG_PARAM = "command";

    @InjectPresenter
    ImageAdjustmentPresenter mPresenter;

    @ProvidePresenter
    ImageAdjustmentPresenter provideImageAdjustmentPresenter() {
        return new ImageAdjustmentPresenter(getArguments());
    }
    
    @BindView(R.id.minValueTextView)
    TextView minValueTextView;

    @BindView(R.id.currentValueTextView)
    TextView currentValueTextView;

    @BindView(R.id.maxValueTextView)
    TextView maxValueTextView;

    @BindView(R.id.toolSeekBar)
    DiscreteSeekBar toolSeekBar;

    private Unbinder mUnbinder;

    private ImageEditorView mImageEditorView;

    public static ImageAdjustmentFragment newInstance(EditorTool editorTool) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, editorTool);

        ImageAdjustmentFragment fragment = new ImageAdjustmentFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public ImageAdjustmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageEditorView =
                (ImageEditorView) getActivity().findViewById(R.id.imageEditorView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slider_control, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        toolSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                currentValueTextView.setText(String.valueOf(value));
                mPresenter.progressChanged(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void changeToolbarTitle(@StringRes int title) {
        ToolbarUtil.updateTitle(title, getActivity());
    }

    @Override
    public void changeToolbarSubtitle(@StringRes int subtitle) {
        ToolbarUtil.updateSubtitle(subtitle, getActivity());
    }

    @Override
    public void onIntensityValueChanged(int value) {
        Log.i("Adjustment", "Filter intensity = " + String.valueOf(value));

        mImageEditorView.setFilterIntensity(value);
    }

    @Override
    public void onBrightnessValueChanged(int value) {
        Log.i("Adjustment", "Brightness = " + String.valueOf(value));

        mImageEditorView.setBrightnessValue(value);
    }

    @Override
    public void onContrastValueChanged(int value) {
        mImageEditorView.setContrastValue(value);
    }

    @Override
    public void onWarmthValueChanged(int value) {
        mImageEditorView.setWarmthValue(value);
    }

    @Override
    public void onStraightenValueChanged(int value) {
        mImageEditorView.setStraightenTransformValue(value);
    }

    @Override
    public void onVignetteValueChanged(int value) {
        mImageEditorView.setVignetteIntensity(value);
    }

    @Override
    public void setupImageEditorCommand(EditorTool command) {
        mImageEditorView.changeTool(command);
    }

    @Override
    public void initializeSlider(int minValue, int maxValue, int value) {
        toolSeekBar.setMin(minValue);
        minValueTextView.setText(String.valueOf(minValue));

        toolSeekBar.setProgress(value);
        currentValueTextView.setText(String.valueOf(value));

        toolSeekBar.setMax(maxValue);
        maxValueTextView.setText(String.valueOf(maxValue));
    }
}