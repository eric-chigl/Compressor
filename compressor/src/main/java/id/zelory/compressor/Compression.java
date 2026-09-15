package id.zelory.compressor;

import java.util.ArrayList;
import java.util.List;

public class Compression {
    private final List<Constraint> constraints = new ArrayList<>();

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }
}
