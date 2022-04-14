package dhbw.smartmoderation.account.contactexchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.UiThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dhbw.smartmoderation.R;

public class QrCodeView extends FrameLayout {

    private final ImageView qrCodeImageView;
    private boolean fullScreen = false;
    private FullScreenListener listener;

    public QrCodeView(@Nonnull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.qr_code_view, this, true);
        qrCodeImageView = findViewById(R.id.qr_code);
        ImageView fullScreenButton = findViewById(R.id.fullscreen_button);
        fullScreenButton.setOnClickListener(v -> {
            fullScreen = !fullScreen;

            if(!fullScreen) {

                fullScreenButton.setImageResource(R.drawable.ic_baseline_fullscreen_24);
            }

            else {

                fullScreenButton.setImageResource(R.drawable.ic_baseline_fullscreen_exit_24);
            }

            if(listener != null) {

                listener.setFullScreen(fullScreen);
            }

        });
    }

    @UiThread
    public void setQrCode(Bitmap qrCode) {
        qrCodeImageView.setImageBitmap(qrCode);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(200);
        qrCodeImageView.startAnimation(animation);
    }

    @UiThread
    public void setFullScreenListener(FullScreenListener listener) {
        this.listener = listener;
    }


    public interface FullScreenListener {

        void setFullScreen(boolean fullScreen);
    }
}
