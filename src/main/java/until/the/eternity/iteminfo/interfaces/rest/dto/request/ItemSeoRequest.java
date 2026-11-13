package until.the.eternity.iteminfo.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemSeoRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 25) String topCategory,
        @NotBlank @Size(max = 25) String subCategory) {}
