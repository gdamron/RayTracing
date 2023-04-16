#ifndef INCLUDES_H
#define INCLUDES_H

#include <math.h>
#include <limits>
#include <memory>
#include <cstdlib>

using std::shared_ptr;
using std::make_shared;

const double RT_INFINITY = std::numeric_limits<double>::infinity();
const double RT_PI = 3.1415926535897932385;

#include "ray.h"
#include "vec3.h"
#include "util.h"

#endif
