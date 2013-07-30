package com.bluntllama.fivekind.items;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.bluntllama.fivekind.R;

public class Die {
    public ImageView imageView;
    public AnimationDrawable anim;
    public int value = 1;
    public boolean isSelected = false;

    public Die(ImageView i) {
        imageView = i;
        imageView.setImageBitmap(null);
        imageView.setBackgroundResource(R.drawable.die_one);
        anim = (AnimationDrawable) imageView.getBackground();
    }

    public void reset() {
        anim.selectDrawable(0);
        imageView.setAlpha(1.0f);
        value = 1;
        isSelected = false;
    }

    public void setEnabled(boolean arg) {
        isSelected = !arg;
        if (arg)
            imageView.setAlpha(1.0f);
        else
            imageView.setAlpha(0.3f);
    }

    public void setValue(int value) {
        this.value = value;
        anim.selectDrawable(value-1);
    }
}