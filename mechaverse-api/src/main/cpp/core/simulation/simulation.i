%module Mechaverse

%include <std_string.i>

%apply(char *STRING, int LENGTH) { (char *data, int len) };

 %{
 #include <core/simulation/Simulation.h>
 %}
 
 %include "Simulation.h"
 