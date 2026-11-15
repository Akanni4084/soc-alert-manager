package com.abdulmanan.repository;

import com.abdulmanan.model.Analyst;

import java.io.IOException;
import java.util.List;

public interface AnalystLoader {

    List<Analyst> load(String resourceName) throws IOException;
}