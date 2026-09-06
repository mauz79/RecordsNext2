var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "August 31 1997" 
dataGiornata[2] = "September 14 1997" 
dataGiornata[3] = "September 21 1997" 
dataGiornata[4] = "September 28 1997" 
dataGiornata[5] = "October 05 1997" 
dataGiornata[6] = "October 19 1997" 
dataGiornata[7] = "November 02 1997" 
dataGiornata[8] = "November 09 1997" 
dataGiornata[9] = "November 23 1997" 
dataGiornata[10] = "November 30 1997" 
dataGiornata[11] = "December 07 1997" 
dataGiornata[12] = "December 14 1997" 
dataGiornata[13] = "December 21 1997" 
dataGiornata[14] = "January 04 1998" 
dataGiornata[15] = "January 11 1998" 
dataGiornata[16] = "January 18 1998" 
dataGiornata[17] = "January 25 1998" 
dataGiornata[18] = "February 01 1998" 
dataGiornata[19] = "February 08 1998" 
dataGiornata[20] = "February 11 1998" 
dataGiornata[21] = "February 15 1998" 
dataGiornata[22] = "February 22 1998" 
dataGiornata[23] = "March 01 1998" 
dataGiornata[24] = "March 08 1998" 
dataGiornata[25] = "March 15 1998" 
dataGiornata[26] = "March 22 1998" 
dataGiornata[27] = "March 29 1998" 
dataGiornata[28] = "April 05 1998" 
dataGiornata[29] = "April 11 1998" 
dataGiornata[30] = "April 19 1998" 
dataGiornata[31] = "April 26 1998" 
dataGiornata[32] = "May 03 1998" 
dataGiornata[33] = "May 10 1998" 
dataGiornata[34] = "May 16 1998" 
function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 1997)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
