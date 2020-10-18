#ifndef color_H
#define color_H

#include "includes.h"
#include <iostream>

void write_color(std::ostream &out, color col, int samples) {
    const double min = 0.0;
    const double max = 0.999;

    auto scale = 1.0 / samples;
    auto r = 256 * clamp(sqrt(col.r() * scale), min, max);
    auto g = 256 * clamp(sqrt(col.g() * scale), min, max);
    auto b = 256 * clamp(sqrt(col.b() * scale), min, max);

    out << static_cast<int>(r) << ' '
        << static_cast<int>(g) << ' '
        << static_cast<int>(b) << '\n';
}

#endif
