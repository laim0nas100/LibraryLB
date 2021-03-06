package lt.lb.commons.io;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public interface DirectoryTreeVisitor extends TreeVisitor<Path> {

    @Override
    public default ReadOnlyIterator<Path> getChildren(Path item) {
        if (Files.isDirectory(item)) {
            return Checked.uncheckedCall(() -> {
                DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(item);

                return ReadOnlyIterator.of(newDirectoryStream.iterator())
                        .withEnsuredCloseOperation(() -> Checked.checkedRun(newDirectoryStream::close));
            });

        } else {
            return ReadOnlyIterator.of();
        }
    }

}
