package ru.muffinnorth.w4j.listeners;

import com.google.common.primitives.Ints;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Optional;
import java.util.function.Consumer;

public class ChangeTargetListener implements ChangeListener<String> {

    private final Consumer<Integer> callback;

    public ChangeTargetListener(Consumer<Integer> callback){
        this.callback = callback;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        Optional<Integer> value = Optional.ofNullable(Ints.tryParse(newValue));
        value.filter(integer -> integer >= 0).ifPresent(callback);
    }
}
