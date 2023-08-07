package com.anvay.noqueuepay.models;

import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import com.denzcoskun.imageslider.constants.ScaleTypes;
@IgnoreExtraProperties
public class SliderImages {
    @Exclude
    private List<SlideModel> images = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();

    @Exclude
    public List<SlideModel> getImages() {
        for (String url : imageUrls) {
            images.add(new SlideModel(url, null, ScaleTypes.FIT));
        }
        return images;
    }

    @Exclude
    public void setImages(List<SlideModel> images) {
        this.images = images;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
