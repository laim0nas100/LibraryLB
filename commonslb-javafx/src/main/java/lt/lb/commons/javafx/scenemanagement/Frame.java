package lt.lb.commons.javafx.scenemanagement;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lt.lb.commons.javafx.FX;

/**
 *
 * @author laim0nas100
 */
public interface Frame {

    public Stage getStage();

    public String getID();

    public String getType();

    public FrameManager getManager();

    public default Scene getScene() {
        return getStage().getScene();
    }

    public default String getTitle() {
        return getStage().getTitle();
    }

    public default void close() {
        FX.submit(() -> {
            getManager().closeFrame(getID());
        });
    }

    public default void show() {
        FX.submit(() -> {
            this.getStage().show();
        });
    }

    public default void hide() {
        FX.submit(() -> {
            this.getStage().hide();
        });
    }
}
