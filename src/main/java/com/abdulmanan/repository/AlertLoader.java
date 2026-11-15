package com.abdulmanan.repository;

import com.abdulmanan.model.Alert;

import java.io.IOException;
import java.util.List;

public interface AlertLoader {

    List<Alert> load(String resourceName) throws IOException;
}