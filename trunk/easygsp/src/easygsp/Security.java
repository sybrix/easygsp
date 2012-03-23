package easygsp;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 1/6/12
 * Time: 9:12 PM
 */
public interface Security {
        boolean isAuthenticated();
        List<String> getRoles();
        String getUsername();
}
