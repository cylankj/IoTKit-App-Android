#pragma version(1)
#pragma rs java_package_name(com.cylan.helper)

uchar4 maskColor = {0, 0, 0, 0};

static uchar mixRGB (uchar src, uchar mask, float inAlpha, float maskAlpha, float outAlpha) {
  return (uchar) (((src * (1 - maskAlpha) + mask * maskAlpha) / (inAlpha + maskAlpha - maskAlpha * inAlpha)) * outAlpha);
}

uchar4 RS_KERNEL mask(uchar4 in) {
  uchar4 out = in;
  float inAlpha = (float)in.a / 255;
  float maskAlpha = (float)maskColor.a / 255;
  float outAlpha = 1 - (1 - maskAlpha) * (1 - inAlpha);

  out.r = mixRGB(in.r, maskColor.r, inAlpha, maskAlpha, outAlpha);
  out.g = mixRGB(in.g, maskColor.g, inAlpha, maskAlpha, outAlpha);
  out.b = mixRGB(in.b, maskColor.b, inAlpha, maskAlpha, outAlpha);
  out.a = (uchar) (outAlpha * 255);
  return out;
}

