package lt.lb.commons.javafx.scenemanagement.frames;

/**
 *
 * @author laim0nas100
 */
public interface FrameState {

    public static class FrameStateClose implements FrameState {

        public static final FrameStateClose instance = new FrameStateClose();

    }

    public static class FrameStateOpen implements FrameState {

        public static final FrameStateOpen instance = new FrameStateOpen();
    }
    
    public static class FrameStateHide implements FrameState {

        public static final FrameStateHide instance = new FrameStateHide();
    }
    
    public static class FrameStateShow implements FrameState {

        public static final FrameStateShow instance = new FrameStateShow();
    }
}
