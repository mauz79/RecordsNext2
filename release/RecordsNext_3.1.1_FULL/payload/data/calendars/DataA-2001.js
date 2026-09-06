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
dataGiornata[1] = "August 26 2001" 
dataGiornata[2] = "September 09 2001" 
dataGiornata[3] = "September 16 2001" 
dataGiornata[4] = "September 23 2001" 
dataGiornata[5] = "September 30 2001" 
dataGiornata[6] = "October 10 2001" 
dataGiornata[7] = "October 14 2001" 
dataGiornata[8] = "October 21 2001" 
dataGiornata[9] = "October 28 2001" 
dataGiornata[10] = "November 04 2001" 
dataGiornata[11] = "November 18 2001" 
dataGiornata[12] = "November 25 2001" 
dataGiornata[13] = "December 02 2001" 
dataGiornata[14] = "December 09 2001" 
dataGiornata[15] = "December 16 2001" 
dataGiornata[16] = "December 23 2001" 
dataGiornata[17] = "January 06 2002" 
dataGiornata[18] = "January 13 2002" 
dataGiornata[19] = "January 20 2002" 
dataGiornata[20] = "January 27 2002" 
dataGiornata[21] = "February 03 2002" 
dataGiornata[22] = "February 10 2002" 
dataGiornata[23] = "February 17 2002" 
dataGiornata[24] = "February 24 2002" 
dataGiornata[25] = "March 03 2002" 
dataGiornata[26] = "March 10 2002" 
dataGiornata[27] = "March 17 2002" 
dataGiornata[28] = "March 24 2002" 
dataGiornata[29] = "March 30 2002" 
dataGiornata[30] = "April 07 2002" 
dataGiornata[31] = "April 14 2002" 
dataGiornata[32] = "April 21 2002" 
dataGiornata[33] = "April 28 2002" 
dataGiornata[34] = "May 05 2002" 
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
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
