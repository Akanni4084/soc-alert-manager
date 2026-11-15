package com.abdulmanan.repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public class BinaryStateRepository implements StateRepository {

    private final Path file;

    public BinaryStateRepository(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath();
    }

    @Override
    public Optional<ApplicationState> load() throws IOException {
        if (Files.notExists(file)) {
            return Optional.empty();
        }

        try (ObjectInputStream input = new ObjectInputStream(
                Files.newInputStream(file)
        )) {
            Object object = input.readObject();

            if (!(object instanceof ApplicationState state)) {
                throw new IOException(
                        "Le fichier ne contient pas une sauvegarde valide."
                );
            }

            return Optional.of(state);
        } catch (ClassNotFoundException exception) {
            throw new IOException(
                    "Impossible de reconnaître les objets sauvegardés.",
                    exception
            );
        }
    }

    @Override
    public void save(ApplicationState state) throws IOException {
        Objects.requireNonNull(state, "Les données sont obligatoires.");

        Files.createDirectories(file.getParent());

        Path temporaryFile = Files.createTempFile(
                file.getParent(),
                "soc-state-",
                ".tmp"
        );

        try {
            try (ObjectOutputStream output = new ObjectOutputStream(
                    Files.newOutputStream(temporaryFile)
            )) {
                output.writeObject(state);
            }

            try {
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}