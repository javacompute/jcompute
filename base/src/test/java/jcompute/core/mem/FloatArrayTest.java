package jcompute.core.mem;

import java.lang.foreign.Arena;
import java.util.stream.IntStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.RequiredArgsConstructor;

import jcompute.core.shape.Shape;

class FloatArrayTest {

    @RequiredArgsConstructor
    enum Scenario {
        N10K(10_000, 1.6666641E9f, 1E6f),
        N511(511, 222387.08f, 0.2f),
        N512(512, 223695.22f, 0.2f),
        N513(513, 225008.50f, 0.2f);
        final int n;
        final float expected;
        final float tolerance;
        void verify(final float dotProduct) {
            assertEquals(expected,  dotProduct, tolerance);
        }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void dotProduct(final Scenario scenario) {
        final int n = scenario.n;
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.of(arena, Shape.of(n));
            var b = FloatArray.of(arena, Shape.of(n));
            IntStream.range(0, n)
            .forEach(i->{
                a.put(i, 0.1f*i);
                b.put(i, 0.1f*(n-i));
            });
            scenario.verify(a.dotProduct(b));
        }
    }

}
