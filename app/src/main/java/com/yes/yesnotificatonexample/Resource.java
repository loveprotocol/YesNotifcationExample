package com.yes.yesnotificatonexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


@SuppressWarnings({"ConstantConditions"})
public final class Resource<T> {
    @Nullable
    private final T data;
    @Nullable
    private final Exception error;
    @Nullable
    private final Boolean endPointOfData;

    public Resource(@NonNull T data) {
        this(data, null, null);
    }

    public Resource(@NonNull T data, @NonNull Boolean endPointOfData) {
        this(data, null, endPointOfData? true:null);
    }

    public Resource(@NonNull Exception exception) {
        this(null, exception, null);
    }

    public Resource(@NonNull T data, @NonNull Exception exception) { this(data, exception, null);}

    private Resource(@Nullable T value, @Nullable Exception error, @Nullable Boolean endOfData) {
        this.data = value;
        this.error = error;
        this.endPointOfData = endOfData;
    }

    public boolean isSuccessful() {
        return data != null && error == null;
    }

    public boolean isEndPointOfData() { return endPointOfData != null; }

    @NonNull
    public T data() {
        if (error != null) {
            throw new IllegalStateException("error is not null. Call isSuccessful() first.");
        }
        return data;
    }

    @NonNull
    public Exception error() {
        if (data != null) {
            throw new IllegalStateException("data is not null. Call isSuccessful() first.");
        }
        return error;
    }
}
