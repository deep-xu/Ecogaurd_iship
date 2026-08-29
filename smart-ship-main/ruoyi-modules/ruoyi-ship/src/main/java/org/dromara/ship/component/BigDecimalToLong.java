package org.dromara.ship.component;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author lyn
 */
@Component
public class BigDecimalToLong {

    public Long convert(BigDecimal bigDecimal) {

        return bigDecimal.longValue();
    }
}
