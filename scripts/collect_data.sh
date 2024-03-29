# Check if the number of arguments is correct
if [ $# -ne 1 ]; then
    echo "Usage: $0 <remote password>"
    exit 1
fi

# Check if the nodes.txt file exists
if [ ! -f nodes.txt ]; then
    echo "File nodes.txt does not exist"
    exit 1
fi

mkdir ~/lyra

# Copy the data.txt file from each of the nodes using sshpass in silent mode. The copied files are renamed to data_<node index>.txt
while read -r line; do
    sshpass -p "$1" scp root@$line:~/data.txt ~/lyra/data_$line.txt
done < nodes.txt

echo "Data collected"

# Zip all the data files into a file called data.zip
zip data.zip ~/lyra/data_*.txt

echo "Data zipped"

# Remove all the data files
rm ~/lyra/data_*.txt

rmdir ~/lyra

exit 0
