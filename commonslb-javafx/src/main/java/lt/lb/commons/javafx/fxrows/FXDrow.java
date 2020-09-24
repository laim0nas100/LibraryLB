package lt.lb.commons.javafx.fxrows;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lt.lb.commons.rows.SyncDrow;
import lt.lb.commons.javafx.fxrows.FXSync;

/**
 * @author laim0nas100
 */
public class FXDrow extends SyncDrow<FXCell, Node, FXLine, FXUpdates, FXDrowConf, FXDrow> {

    public FXDrow(FXLine line, FXDrowConf config, String key) {
        super(line, config, key);
    }

    public FXDrow addButton(String str, EventHandler<ActionEvent> eh) {
        Button b = new Button(str);
        b.setOnAction(eh);
        return add(b);
    }

    public FXDrow addLabel(String str) {
        return add(new Label(str));
    }

    public FXDrow addFxSync(FXSync sync) {
        addSync(sync);
        addSyncValidation(sync);
        return add(sync.getNode());
    }

    @Override
    public FXDrow me() {
        return this;
    }

}