package de.adesso.gitstalker.core.resources.repository_Resources;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class Repositories {

    private PageInfo pageInfo;
    private ArrayList<NodesRepositories> nodes;
}
