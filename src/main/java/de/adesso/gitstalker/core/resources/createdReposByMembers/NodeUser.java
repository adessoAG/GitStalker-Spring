package de.adesso.gitstalker.core.resources.createdReposByMembers;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NodeUser {

    private String id;
    private UserRepositories repositories;
}
