#ifndef UTILS_H
#define UTILS_H

#include "includes.h"

inline double degrees_to_rads(double degrees) {
    return degrees * RT_PI / 180.0;
}

inline double random_double() {
    return rand() / (RAND_MAX + 1.0);
}

inline double random_double(double min, double max) {
    return min + (max - min) * random_double();
}

inline double clamp(double x, double min, double max) {
    if (x < min) {
        return min;
    }

    if (x > max) {
        return max;
    }

    return x;
}
#endif
