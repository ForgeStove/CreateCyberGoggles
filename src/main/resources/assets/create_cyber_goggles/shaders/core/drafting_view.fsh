#version 150

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D Palette;
uniform sampler2D Dither;
uniform vec4 LineColor;
uniform vec4 LineShadowColor;
uniform float PaletteOffset;
uniform vec2 InSize;
uniform float Pixelate;
uniform float PixelScale;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

const int bit_depth = 32;
const float contrast = 1.2;
const float offset = 0.18f;

vec2 getVirtualSize() {
    if (Pixelate < 0.5) return InSize;
    return floor(InSize / max(PixelScale, 1.0));
}

vec2 pixelate(vec2 uv) {
    if (Pixelate < 0.5) return uv;
    vec2 vs = getVirtualSize();
    return (floor(uv * vs) + 0.5) / vs;
}

vec4 paletted_dither(vec2 pixel_uv, vec3 screen_col, sampler2D palette) {
    float luminosity = (screen_col.r * 0.299) + (screen_col.g * 0.587) + (screen_col.b * 0.114);
    luminosity *= 0.7;

    luminosity = (luminosity - 0.5 + offset) * contrast + 0.5;
    luminosity = clamp(luminosity, 0.0, 1.0);

    float bits = float(bit_depth);
    luminosity = floor(luminosity * bits) / bits;

    ivec2 col_sizei = textureSize(palette, 0);
    vec2 col_size = vec2(col_sizei);
    col_size *= 1.5;

    float col_x = float(col_size.x) - 1.0;
    float col_texel_size = 1.0 / col_x;

    luminosity = max(luminosity - 0.00001, 0.0);
    float luminosity_lower = floor(luminosity * col_x) * col_texel_size;
    float luminosity_upper = (floor(luminosity * col_x) + 1.0) * col_texel_size;
    float luminosity_scaled = luminosity * col_x - floor(luminosity * col_x);

    ivec2 dither_size = textureSize(Dither, 0);
    vec2 inv_dither_size = vec2(1.0 / float(dither_size.x), 1.0 / float(dither_size.y));
    vec2 dither_uv = pixel_uv * getVirtualSize() * inv_dither_size;
    float threshold = texture(Dither, dither_uv).r;

    threshold = threshold * 0.99 + 0.005;

    float ramp_value = step(threshold, luminosity_scaled);
    float col_sample = min(mix(luminosity_lower, luminosity_upper, ramp_value), 1.0);
    return texture(palette, vec2(col_sample, PaletteOffset));
}

vec4 outline_color(vec2 pixel_uv) {
    float outline_fading = 1.04f;
    vec2 pixel_texel = 1.0 / getVirtualSize();
    vec4 center = texture(DiffuseDepthSampler, pixel_uv);
    vec4 left = texture(DiffuseDepthSampler, pixel_uv - vec2(pixel_texel.x, 0.0));
    vec4 right = texture(DiffuseDepthSampler, pixel_uv + vec2(pixel_texel.x, 0.0));
    vec4 up = texture(DiffuseDepthSampler, pixel_uv - vec2(0.0, pixel_texel.y));
    vec4 down = texture(DiffuseDepthSampler, pixel_uv + vec2(0.0, pixel_texel.y));
    float leftDiff = pow(abs(center.r - left.r), outline_fading);
    float rightDiff = pow(abs(center.r - right.r), outline_fading);
    float upDiff = pow(abs(center.r - up.r), outline_fading);
    float downDiff = pow(abs(center.r - down.r), outline_fading);
    float total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);

    total = min(total * 20.0, 1.0);

    vec4 lineColor = LineColor;
    if (leftDiff > rightDiff) lineColor = LineShadowColor;
    if (upDiff > downDiff) lineColor = LineShadowColor;
    return vec4(lineColor.rgb, total * lineColor.a);
}

void main() {
    vec2 pUV = pixelate(texCoord);

    float depth = texture(DiffuseDepthSampler, pUV).r;
    vec3 color = texture(DiffuseSampler0, pUV).rgb;
    vec4 dither = paletted_dither(pUV, color, Palette);
    vec4 outline = outline_color(pUV);

    vec3 paperColor = texture(Palette, vec2(1, PaletteOffset)).rgb;
    float isSky = step(0.9999999, depth);

    vec3 result = mix(dither.rgb, outline.rgb, outline.a);
    result = mix(result, paperColor, isSky);
    fragColor = vec4(result, 1.0);
}
