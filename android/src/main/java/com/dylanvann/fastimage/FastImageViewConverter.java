package com.dylanvann.fastimage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.util.HashMap;
import java.util.Map;

class FastImageViewConverter {
    private static final Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    private static final Map<String, FastImageCacheControl> FAST_IMAGE_CACHE_CONTROL_MAP =
            new HashMap<String, FastImageCacheControl>() {{
                put("immutable", FastImageCacheControl.IMMUTABLE);
                put("web", FastImageCacheControl.WEB);
                put("cacheOnly", FastImageCacheControl.CACHE_ONLY);
            }};

    private static final Map<String, Priority> FAST_IMAGE_PRIORITY_MAP =
            new HashMap<String, Priority>() {{
                put("low", Priority.LOW);
                put("normal", Priority.NORMAL);
                put("high", Priority.HIGH);
            }};

    private static final Map<String, PorterDuff.Mode> FAST_IMAGE_BLEND_MODE_MAP =
            new HashMap<String, PorterDuff.Mode>() {{
                put("overlay", PorterDuff.Mode.OVERLAY);
            }};

    private static final Map<String, ImageView.ScaleType> FAST_IMAGE_RESIZE_MODE_MAP =
            new HashMap<String, ImageView.ScaleType>() {{
                put("contain", ScaleType.FIT_CENTER);
                put("cover", ScaleType.CENTER_CROP);
                put("stretch", ScaleType.FIT_XY);
                put("center", ScaleType.CENTER);
            }};

    // Resolve the source uri to a file path that android understands.
    static FastImageSource getImageSource(Context context, ReadableMap source) {
        return new FastImageSource(context, source.getString("uri"), getHeaders(source));
    }

    static FastImageGradient getImageGradient(Context context, ReadableMap gradient) {
        return new FastImageGradient(context, getColors(gradient), getBlendMode(gradient), getLocations(gradient), gradient.getInt("angle"));
    }

    static Headers getHeaders(ReadableMap source) {
        Headers headers = Headers.DEFAULT;

        if (source.hasKey("headers")) {
            ReadableMap headersMap = source.getMap("headers");
            ReadableMapKeySetIterator iterator = headersMap.keySetIterator();
            LazyHeaders.Builder builder = new LazyHeaders.Builder();

            while (iterator.hasNextKey()) {
                String header = iterator.nextKey();
                String value = headersMap.getString(header);

                builder.addHeader(header, value);
            }

            headers = builder.build();
        }

        return headers;
    }

    static RequestOptions getOptions(ReadableMap source) {
        // Get priority.
        final Priority priority = FastImageViewConverter.getPriority(source);
        // Get cache control method.
        final FastImageCacheControl cacheControl = FastImageViewConverter.getCacheControl(source);
        DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.AUTOMATIC;
        Boolean onlyFromCache = false;
        Boolean skipMemoryCache = false;
        switch (cacheControl) {
            case WEB:
                // If using none then OkHttp integration should be used for caching.
                diskCacheStrategy = DiskCacheStrategy.NONE;
                skipMemoryCache = true;
                break;
            case CACHE_ONLY:
                onlyFromCache = true;
                break;
            case IMMUTABLE:
                // Use defaults.
                break;
        }
        return new RequestOptions()
                .diskCacheStrategy(diskCacheStrategy)
                .onlyRetrieveFromCache(onlyFromCache)
                .skipMemoryCache(skipMemoryCache)
                .priority(priority)
                .placeholder(TRANSPARENT_DRAWABLE);
    }

    private static FastImageCacheControl getCacheControl(ReadableMap source) {
        return getValueFromSource("cache", "immutable", FAST_IMAGE_CACHE_CONTROL_MAP, source);
    }

    private static Priority getPriority(ReadableMap source) {
        return getValueFromSource("priority", "normal", FAST_IMAGE_PRIORITY_MAP, source);
    }

    static ScaleType getScaleType(String propValue) {
        return getValue("resizeMode", "cover", FAST_IMAGE_RESIZE_MODE_MAP, propValue);
    }

    private static PorterDuff.Mode getBlendMode(ReadableMap gradient) {
        return getValueFromSource("blendMode", "overlay", FAST_IMAGE_BLEND_MODE_MAP, gradient);
    }

    private static int[] getColors(ReadableMap gradient) {
        ReadableArray propValue = gradient.getArray("colors");
        int[] colors = new int[propValue.size()];
        for (int i = 0, size = propValue.size(); i < size; i++) {
            colors[i] = propValue.getInt(i);
        }
        return colors;
    }

    private static float[] getLocations(ReadableMap gradient) {
        ReadableArray propValue = gradient.getArray("locations");
        float[] locations = new float[propValue.size()];
        for (int i = 0, size = propValue.size(); i < size; i++) {
            locations[i] = (float) propValue.getDouble(i);
        }
        return locations;
    }

    private static <T> T getValue(String propName, String defaultPropValue, Map<String, T> map, String propValue) {
        if (propValue == null) propValue = defaultPropValue;
        T value = map.get(propValue);
        if (value == null)
            throw new JSApplicationIllegalArgumentException("FastImage, invalid " + propName + " : " + propValue);
        return value;
    }

    private static <T> T getValueFromSource(String propName, String defaultProp, Map<String, T> map, ReadableMap source) {
        String propValue;
        try {
            propValue = source != null ? source.getString(propName) : null;
        } catch (NoSuchKeyException e) {
            propValue = null;
        }
        return getValue(propName, defaultProp, map, propValue);
    }
}
