import PIL.Image as pim

def generate_board(width, white, black, outfile):
    new = pim.new("RGB", (width, width))
    for i in range(width):
        i_chunk = int(i/width * 8) % 2 == 0
        for j in range(width):
            j_chunk = int(j/width * 8) % 2 == 0 
            if i_chunk ^ j_chunk:
                pixel = black
            else:
                pixel = white
            new.putpixel((i, j), pixel)
    new.save(outfile)

if __name__ == "__main__":
    WHITE = (255,220,131)
    BLACK = (131,80,40)
    generate_board(800, WHITE, BLACK, "../board/board.png")
