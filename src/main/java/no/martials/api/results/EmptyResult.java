package no.martials.api.results;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Schema(name = "EmptyResult", description = "Empty result with a status")
public abstract class EmptyResult {

    @NotBlank
    @NotNull
    protected final String status;
    @Nullable
    protected String version;

    public EmptyResult(@Nullable final String version) {
        this.status = "OK";
        this.version = version;
    }

    @NotNull
    public String getStatus() {
        return status;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "EmptyResult{" +
                "status=" + status +
                '}';
    }
}
