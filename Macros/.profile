# default .profile
if test "$(tty)" != "not a tty"; then
echo 'edit the file .profile if you want to change your environment.'
echo 'To start the Photon windowing environment, type "ph".'
fi
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/root/j9/bin:/root/j9/lib