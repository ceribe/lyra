# Check if the nodes.txt file exists
if [ ! -f nodes.txt ]; then
    echo "File nodes.txt does not exist"
    exit 1
fi

# Copy the data.txt file from each of the nodes using sshpass in silent mode using the password "password". The copied
# should be renamed to data_<node index>.txt
while read -r line; do
    sshpass -p "password" scp root@$line:~/data.txt data_$line.txt
done < nodes.txt

# Zip all the data files into a file called data.zip
zip data.zip data_*.txt

# Remove all the data files
rm data_*.txt

exit 0