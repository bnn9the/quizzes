package edu.platform.modules.user.api;

import edu.platform.modules.user.entity.User;

/**
 * Facade that exposes user lookup operations to other modules.
 */
public interface UserAccessFacade {

    User getUserById(Long id);

    User getUserByEmail(String email);
}
