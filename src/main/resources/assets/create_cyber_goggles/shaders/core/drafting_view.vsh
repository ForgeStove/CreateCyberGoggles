#version 150

in vec3 Position;

uniform vec2 InSize;

out vec2 texCoord;
out vec2 oneTexel;

void main() {
    gl_Position = vec4(Position, 1.0);
    texCoord = Position.xy * 0.5 + 0.5;
    oneTexel = 1.0 / InSize;
}
