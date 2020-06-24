package lt.lb.commons.jobs;

/**
 *
 * Dependency with explicit job association. 
 * @author laim0nas100
 */
public interface JobDependency<T> extends Dependency {

    

    /**
     * Job that comes with dependency
     * @return 
     */
    public Job<T> getJob();
}
