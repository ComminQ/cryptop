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

      if indicator_name == "EMA" or indicator_name == "SMA":
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
  coolors = ["c-", "r-", "b-", "y-", "g-", "m-", "k-"]
  def pick_color() -> str:
    return coolors.pop(0)


  # Plot
  f, (a1, a2) =  plt.subplots(2, 1, gridspec_kw={'height_ratios': [3, 1]}, sharex=True)

  a1.plot(time, close, pick_color(), label="Prix de clôture")

  for plot_id, indicators in ploting.items():
    axis : plt.Axes = a1 if plot_id == 0 else a2
    for indicator in indicators:
      if indicator in df:
        axis.plot(time, df[indicator], pick_color(), label=indicator)

  a2.set_xlabel("Date")
  a2.set_ylabel("Bruit")
  a2.set_title("Tendance - Marché = Bruit")
  a2.legend()

  a2.axhline(y = 500, color = 'b', linestyle = '-')
  a2.axhline(y = -500, color = 'b', linestyle = '-')
  # plt.axhline(y = 0.5, color = 'r', linestyle = '-')

  a1.set_xlabel("Date")
  a1.set_ylabel("Prix")
  a1.set_title("Prix de clôture")
  a1.legend()

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