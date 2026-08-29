package org.dromara.host.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface Constants {

    @Getter
    @AllArgsConstructor
    enum SycnStatus {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"),
        SUCCESS(2, "处理成功"),
        FAILED(-1, "处理失败");




        private final Integer code;
        private final String info;
    }
}
