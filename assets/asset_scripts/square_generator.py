import PIL.Image as pim

def generate_square(rgb, name, square_width, margin=1):
    new = pim.new("RGB", (square_width, square_width))
    for i in range(square_width):
        for j in range(square_width):
            if (
                margin <= i < square_width - margin and 
                margin <= j < square_width - margin
                ):
                new.putpixel((i, j), rgb)
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
