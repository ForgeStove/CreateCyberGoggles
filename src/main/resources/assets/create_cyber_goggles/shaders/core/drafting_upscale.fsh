#version 150

uniform sampler2D DiffuseSampler0;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    ivec2 texSize = textureSize(DiffuseSampler0, 0);
    ivec2 coord = ivec2(texCoord * vec2(texSize));
    fragColor = texelFetch(DiffuseSampler0, coord, 0);
}
