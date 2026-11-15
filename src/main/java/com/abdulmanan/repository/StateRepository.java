package com.abdulmanan.repository;

import java.io.IOException;
import java.util.Optional;

public interface StateRepository {

    Optional<ApplicationState> load() throws IOException;

    void save(ApplicationState state) throws IOException;
}