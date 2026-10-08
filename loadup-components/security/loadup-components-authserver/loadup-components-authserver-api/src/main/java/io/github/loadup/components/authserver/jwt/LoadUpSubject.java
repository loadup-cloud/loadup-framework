package io.github.loadup.components.authserver.jwt;

/** Identity contract supplied by a user store to the authorization server. */
public interface LoadUpSubject {
    String subjectId();
}
