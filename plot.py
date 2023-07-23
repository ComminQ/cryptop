import json
from os import listdir
from pandas import DataFrame, read_csv
from matplotlib import pyplot as plt

def get_datas() -> dict[int, list[str]]:
  res = {}
  file = "config.json"
  with open(file, "r") as f:
    # Load json
    data = json.load(f)
    # Get datas
    for indicator in data["indicators"]:
      indicator_name = indicator["name"]
      params = indicator.get("params", {})

      if indicator_name == "EMA" or indicator_name == "SMA" or indicator_name == "RSI":
        indicator_name = indicator_name + str(params.get("period", 50))

      plot_id = indicator["plot"]
      if plot_id not in res:
        res[plot_id] = []
      
      res[plot_id].append(indicator_name)
  return res


def plot(df: DataFrame):
   
  time = df["date"]
  close = df["close"]
  ploting = get_datas()
  coolors = ["blue", "red", "salmon", "orange", "brown", "green", "cyan", "purple", "pink", "magenta", "gray"]
  def pick_color() -> str:
    return coolors.pop(0)
  
  plot_count = len(ploting)
  # generate height ratio for plot count
  # eg : 2 plots => height_ratios = [3, 1]
  # eg : 3 plots => height_ratios = [3, 1, 1]
  # etc...
  height_ratios = [3] + [1] * (plot_count - 1) 


  # Plot
  f, plots =  plt.subplots(plot_count, 1, gridspec_kw={'height_ratios': height_ratios}, sharex=True)

  plots[0].plot(time, close, linestyle="-", color=pick_color(), label="Prix de clôture")

  for plot_id, indicators in ploting.items():
    axis : plt.Axes = plots[plot_id]
    for indicator in indicators:
      if indicator in df:
        axis.plot(time, df[indicator], linestyle="-", color=pick_color(), label=indicator)

  plots[1].set_xlabel("Date")
  plots[1].set_ylabel("Bruit")
  plots[1].set_title("Tendance - Marché = Bruit")
  plots[1].legend()

  plots[1].axhline(y = 500, color = 'b', linestyle = '-')
  plots[1].axhline(y = -500, color = 'b', linestyle = '-')
  # set the max y value to be 1000
  plots[1].set_ylim([-1000, 1000])

  # plt.axhline(y = 0.5, color = 'r', linestyle = '-')

  plots[0].set_xlabel("Date")
  plots[0].set_ylabel("Prix")
  plots[0].set_title("Prix de clôture")
  plots[0].legend()

  f.show()
  plt.show()




if __name__ == "__main__":
  print("Plotting...")

  # List files
  files = listdir("data")
  # Ask user for file to plot
  print("Files:")
  for i in range(len(files)):
    print(f"{(i+1)}: {files[i]}")

  # Get file
  input_str = input("Select file to plot:")
  selected_index = int(input_str)
  if selected_index > len(files) or selected_index < 1:
    print("Quitting ...")
    exit(1)
  file = files[selected_index-1]
  # Open file
  df = read_csv(f"data/{file}")

  # Plot
  plot(df)