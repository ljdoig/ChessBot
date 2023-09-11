import PIL.Image as pim

def generate_square(rgb, name, square_width, margin=0, lighten=False):
    if lighten:
        rgb = tuple(min(255, int(1.1 * i)) for i in rgb)
    new = pim.new("RGB", (square_width, square_width))
    for i in range(square_width):
        for j in range(square_width):
            if (
                margin <= i < square_width - margin and 
                margin <= j < square_width - margin
                ):
                new.putpixel((i, j), rgb)
    if lighten:
        new.save(f"../board/{name}_sqr+.png")
    else:
        new.save(f"../board/{name}_sqr.png")

if __name__ == "__main__":
    colours = [
        ((243, 164, 156), "pink"),
        ((102, 141, 60), "green"),
        ((83, 114, 175), "blue"),
        ((200, 200, 200), "ntrl")
    ] 
    for rgb, name in colours:
        generate_square(rgb, name, 800 // 8)
        generate_square(rgb, name, 800 // 8, lighten=True)
