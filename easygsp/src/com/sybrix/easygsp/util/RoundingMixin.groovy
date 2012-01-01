package com.sybrix.easygsp.util

class RoundingMixin {
        public BigDecimal round(int n) {
                return setScale(n, BigDecimal.ROUND_HALF_UP);
        }

}
